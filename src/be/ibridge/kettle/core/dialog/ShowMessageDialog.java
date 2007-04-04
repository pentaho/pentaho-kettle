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

package be.ibridge.kettle.core.dialog;

import java.util.ArrayList;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.trans.step.BaseStepDialog;

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
    private Props props;

    private int flags;
    private int returnValue;

    private Shell parent;

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
        
        props = Props.getInstance();
    }

    public int open()
    {
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM);
        props.setLook(shell);

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
            fdlDesc.top = new FormAttachment(wIcon, 0, SWT.CENTER);
        }
        else
        {
            fdlDesc.left = new FormAttachment(0, 0);
            fdlDesc.top = new FormAttachment(0, margin);
        }
        fdlDesc.right = new FormAttachment(100, 0);
        wlDesc.setLayoutData(fdlDesc);
        

        List buttons = new ArrayList();
        if ( (flags & SWT.OK) !=0)
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.OK"));
            button.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.OK); } });
            buttons.add(button);
        }
        if ( (flags & SWT.CANCEL) !=0) 
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.Cancel"));
            button.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.CANCEL); } });
            buttons.add(button);
        }
        if ( (flags & SWT.YES) !=0)
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.Yes"));
            button.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.YES); } });
            buttons.add(button);
        }
        if ( (flags & SWT.NO) !=0) 
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(Messages.getString("System.Button.No"));
            button.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.NO); } });
            buttons.add(button);
        }
        
        BaseStepDialog.positionBottomButtons(shell, (Button[]) buttons.toArray(new Button[buttons.size()]), margin, wlDesc);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        shell.pack();
        
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
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

}
