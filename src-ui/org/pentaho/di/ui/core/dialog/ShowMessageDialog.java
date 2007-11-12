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
import org.pentaho.di.ui.core.gui.GUIResource;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Dialog to enter a text. (descriptions etc.)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class ShowMessageDialog extends Dialog
{
    private String title, message;

    private Shell shell;
    private PropsUI props;

    private int flags;
    private int returnValue;

    private Shell parent;
    
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
        super(parent, SWT.NONE);
        this.parent = parent;
        this.flags = flags;
        this.title = title;
        this.message = message;
        
        props = PropsUI.getInstance();
    }

    public int open()
    {
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM);
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
        Label wlDesc = new Label(shell, SWT.NONE);
        wlDesc.setText(message);
        props.setLook(wlDesc);
        FormData fdlDesc = new FormData();
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
        fdlDesc.right = new FormAttachment(100, 0);
        wlDesc.setLayoutData(fdlDesc);
        

        buttons = new ArrayList<Button>();
        adapters = new ArrayList<SelectionAdapter>();
        
        if ( (flags & SWT.OK) !=0)
        {
            Button button = new Button(shell, SWT.PUSH);
            final String ok = Messages.getString("System.Button.OK"); 
            button.setText(ok);
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.OK); } }; 
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        if ( (flags & SWT.CANCEL) !=0) 
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.Cancel"));
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.CANCEL); } };
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        if ( (flags & SWT.YES) !=0)
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.Yes"));
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.YES); } };
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        if ( (flags & SWT.NO) !=0) 
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.No"));
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.NO); } };
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        
        BaseStepDialog.positionBottomButtons(shell, buttons.toArray(new Button[buttons.size()]), margin, wlDesc);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        shell.layout();
        shell.pack(true);
        
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
                    button.setText(ok+" ("+(timeOut-diff)+")");
                    
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
